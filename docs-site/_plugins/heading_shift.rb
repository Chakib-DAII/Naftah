require "nokogiri"

module Jekyll
  module HeadingShift
    def shift_headings(input)
      return input if input.nil? || input.empty?

      doc = Nokogiri::HTML::DocumentFragment.parse(input)

      doc.css("h1, h2, h3, h4, h5, h6").each do |node|
        level = node.name[1].to_i
        new_level = level + 1
        new_level = 6 if new_level > 6
        node.name = "h#{new_level}"
      end

      doc.to_html
    end
  end
end

Liquid::Template.register_filter(Jekyll::HeadingShift)