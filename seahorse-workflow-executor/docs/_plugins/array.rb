module ArrayFilter
  def indexof(array, item)
    array.index(item)
  end
end
Liquid::Template.register_filter(ArrayFilter)
