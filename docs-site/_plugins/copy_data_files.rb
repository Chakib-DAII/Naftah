require "fileutils"
require "zlib"
require "json"

Jekyll::Hooks.register :site, :post_write do |site|
  # Read files to process from config, fallback to empty array if not defined
  files_to_process = site.config["copy_data_files"] || []

  src_dir  = File.join(site.source, "_data")
  dest_dir = File.join(site.dest, "assets", "data")

  FileUtils.mkdir_p(dest_dir)

  CHUNK_SIZE = (site.config["data_chunk_size"] || 100).to_i

  files_to_process.each do |entry|
    # Resolve entry config; Entry can be a String OR an Object
    if entry.is_a?(String)
      filename = entry
      compress = false
      chunk    = false

    elsif entry.is_a?(Hash)
      filename = entry["file"]
      compress = entry.fetch("compress", false)  # default = false
      chunk    = entry.fetch("chunk", false)  # default = false
    else
      Jekyll.logger.warn "DataPipeline:", "Invalid entry: #{entry.inspect}"
      next
    end

    src = File.join(src_dir, filename)

    unless File.exist?(src)
      Jekyll.logger.warn "DataPipeline:", "Missing file in _data: #{filename}"
      next
    end

    base_name = File.basename(filename, ".json")

    # CHUNKED MODE + INDEXING
    if chunk

      begin
        data = JSON.parse(File.read(src))

        unless data.is_a?(Array)
          Jekyll.logger.warn "DataPipeline:", "#{filename} is not an array, skipping chunking"
          next
        end

        total_pages = (data.size.to_f / CHUNK_SIZE).ceil

        Jekyll.logger.info "DataPipeline:", "Chunking #{filename} → #{total_pages} pages (#{CHUNK_SIZE}/page)"

        # INVERTED INDEX
        inverted_index = Hash.new { |h, k| h[k] = [] }

        data.each_with_index do |item, global_id|

          tokens = [
            item["className"],
            item["methodName"],
            item["qualifiedCall"]
          ]
          .compact
          .flat_map { |v| v.downcase.split(/[^a-z0-9]+/) }
          .reject(&:empty?)
          .uniq

          tokens.each do |token|
            inverted_index[token] << global_id + 1
          end
        end

        # WRITE CHUNKS
        data.each_slice(CHUNK_SIZE).with_index do |slice, index|
          page_num = index + 1
          padded   = page_num.to_s.rjust(4, '0')

          out_name = "#{base_name}-page-#{padded}.json"
          out_path = File.join(dest_dir, out_name)

          json_str = JSON.generate(slice)

          if compress
            gz_path = "#{out_path}.gz"

            Zlib::GzipWriter.open(gz_path, Zlib::BEST_COMPRESSION) do |gz|
              gz.write(json_str)
            end

            Jekyll.logger.info "DataPipeline:", "Wrote #{out_name}.gz (#{slice.size} items)"
          else
            File.write(out_path, json_str)
            Jekyll.logger.info "DataPipeline:", "Wrote #{out_name} (#{slice.size} items)"
          end
        end

        # META FILE
        meta = {
          file: filename,
          total_items: data.size,
          chunk_size: CHUNK_SIZE,
          pages: total_pages
        }

        # WRITE META
        meta_path = File.join(dest_dir, "#{base_name}-meta.json")
        File.write(meta_path, JSON.pretty_generate(meta))

        Jekyll.logger.info "DataPipeline:", "Wrote meta file for #{filename}"

        # WRITE INVERTED INDEX
		index_path = File.join(dest_dir, "#{base_name}-search-index.json.gz")

		Zlib::GzipWriter.open(index_path, Zlib::BEST_COMPRESSION) do |gz|
		  gz.write(JSON.generate(inverted_index))
		end

        Jekyll.logger.info "DataPipeline:", "Wrote compressed search index (gzipped) (#{inverted_index.keys.size} tokens)"

      rescue => e
        Jekyll.logger.error "DataPipeline:", "Failed: #{e.message}"
      end

    # MODE 2: SIMPLE COPY + OPTIONAL GZIP
    else

      dest = File.join(dest_dir, filename)
      gz_dest = "#{dest}.gz"

      FileUtils.cp(src, dest)
      Jekyll.logger.info "DataPipeline:", "Copied #{filename}"

      # Compress if requested
      if compress
        begin
		  Jekyll.logger.info "CopyData:", "Compressing #{filename} → #{filename}.gz"

          content = File.binread(src)

          Zlib::GzipWriter.open(gz_dest, Zlib::BEST_COMPRESSION) do |gz|
            gz.write(content)
          end

          # Remove uncompressed version from site output
          FileUtils.rm_f(dest)

          Jekyll.logger.info "DataPipeline:", "Compressed #{filename} → #{filename}.gz"

        rescue => e
          Jekyll.logger.error "DataPipeline:", "Compression failed for #{filename}: #{e.message}"
        end
	  else
		  Jekyll.logger.info "DataPipeline:", "Compression disabled for #{filename}"
      end
    end
  end
end