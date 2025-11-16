require "fileutils"

Jekyll::Hooks.register :site, :post_write do |site|
  # Read files to copy from config, fallback to empty array if not defined
  files_to_copy = site.config["copy_data_files"] || []


  src_dir  = File.join(site.source, "_data")
  dest_dir = File.join(site.dest, "assets", "data")

  FileUtils.mkdir_p(dest_dir)

  files_to_copy.each do |filename|
    src  = File.join(src_dir, filename)
    dest = File.join(dest_dir, filename)

    if File.exist?(src)
      FileUtils.cp(src, dest)
      Jekyll.logger.info "CopyData:", "Copied #{filename} → assets/data/"
    else
      Jekyll.logger.warn "CopyData:", "File not found in _data: #{filename}"
    end
  end
end
