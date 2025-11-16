require "fileutils"
require "zlib"

Jekyll::Hooks.register :site, :post_write do |site|
  # Read files to copy from config, fallback to empty array if not defined
  files_to_copy = site.config["copy_data_files"] || []

  src_dir  = File.join(site.source, "_data")
  dest_dir = File.join(site.dest, "assets", "data")

  FileUtils.mkdir_p(dest_dir)

  files_to_copy.each do |entry|
    # Entry can be a String OR an Object
    if entry.is_a?(String)
      filename = entry
      compress = false
    elsif entry.is_a?(Hash)
      filename = entry["file"]
      compress = entry.fetch("compress", false)  # default = false
    else
      Jekyll.logger.warn "CopyData:", "Invalid entry: #{entry.inspect}"
      next
    end

    src     = File.join(src_dir, filename)
    dest    = File.join(dest_dir, filename)
    gz_dest = "#{dest}.gz"

    unless File.exist?(src)
      Jekyll.logger.warn "CopyData:", "File not found in _data: #{filename}"
      next
    end

    # Copy file
    FileUtils.cp(src, dest)
    Jekyll.logger.info "CopyData:", "Copied #{filename} → assets/data/"

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
        Jekyll.logger.info "CopyData:", "Removed #{filename}, kept #{filename}.gz"

      rescue => e
        Jekyll.logger.error "CopyData:", "Compression failed for #{filename}: #{e.message}"
      end
    else
      Jekyll.logger.info "CopyData:", "Compression disabled for #{filename}"
    end
  end
end
