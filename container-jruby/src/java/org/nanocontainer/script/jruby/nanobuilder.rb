#--
#############################################################################
# Copyright (C) NanoContainer Organization. All rights reserved.            #
# ------------------------------------------------------------------------- #
# The software in this package is published under the terms of the BSD      #
# style license a copy of which has been included with this distribution in #
# the LICENSE.txt file.                                                     #
#                                                                           #
#############################################################################
#++

#
# Nano::Builder is a domain-specific language for use with JRuby and
# Nanocontainer for configuring containers and components.
#
module Nano
  Parameter = org.picocontainer.Parameter
  DefaultNanoPicoContainer = org.nanocontainer.reflection.DefaultNanoPicoContainer
  ComponentParameter = org.picocontainer.defaults.ComponentParameter
  ConstantParameter = org.picocontainer.defaults.ConstantParameter
  JRubyContainerBuilder = org.nanocontainer.script.jruby.JRubyContainerBuilder
  ClassPathElementHelper = org.nanocontainer.script.ClassPathElementHelper
  ComponentElementHelper = org.nanocontainer.script.ComponentElementHelper

  MARKUP_EXCEPTION_PREFIX = JRubyContainerBuilder::MARKUP_EXCEPTION_PREFIX

  class Key
    def initialize(key = nil)
      @key = key
    end

    def build() ComponentParameter.new(@key) end
  end

  class Constant
    def initialize(val)
      @const = val
    end

    def build() ConstantParameter.new(@const) end
  end

  class Params
    def initialize(params = [])
      @params = [params].flatten.map do |p|
        p.respond_to?(:build) ? p : Constant.new(p)
      end
    end

    def build
      params = Parameter[].new(@params.length)
      @params.each_with_index {|p, i| params[i] = p.build }
      params
    end
  end

  class Component
    def initialize(options)
      @key, @klass, @instance, @cnkey = nil, nil, nil, nil
      if options.kind_of?(Hash)
        @instance = options[:instance]
        @key      = options[:key]
        @klass    = options[:class]
        @params   = options[:parameters]
        @cnkey = options[:classNameKey]
      else
        @klass = options
      end
      @instance or @klass or raise "#{MARKUP_EXCEPTION_PREFIX}either :class or :instance required"
    end

    def build(container)

      parms = Params.new(@params).build if @params
      ComponentElementHelper.makeComponent(@cnkey, @key, parms, @klass, container, @instance)

#      args = [@key]
#      if @instance
#        method = :registerComponentInstance
#        args << @instance
#      else
#        method = :registerComponentImplementation
#        args << @klass
#      end
#      args << Params.new(@params).build if @params
#      args.delete(nil)
#      container.send(method, *args)
    end
  end

  class Container
    def initialize(options = {}, &block)
      @impl     = DefaultNanoPicoContainer
      @parent   = options[:parent]
      @caf      = options[:component_adapter_factory]
      @monitor  = options[:component_monitor]
      @comps    = []
      @children = []
    end

    def build(&block)
      @container = construct_container
      instance_eval(&block) if block
      @comps.each {|comp| comp.build(@container)}
      @container
    end

    def key(key)
      Key.new(key)
    end

    def constant(val)
      Constant.new(val)
    end

    def component(options)
      @comps << Component.new(options)
    end

    def container(options = {}, &block)
      if !options[:parent].nil? && options[:parent].equal?($parent)
          raise "#{MARKUP_EXCEPTION_PREFIX}You can't explicitly specify a parent in a child element."
      end
      if options[:parent].nil?
        options[:parent] = @container
      end
      container = Container.new(options)
      container.build(&block)
      container
    end

    def classPathElement(options = {}, &block)
      cpe = ClassPathElement.new(ClassPathElementHelper.addClassPathElement(options[:path], @container), @container)
      cpe.build(&block)
      cpe
    end

    class ClassPathElement
      def initialize(classPathElement, container)
        @container = container
        @classPathElement = classPathElement
      end
      def build(&block)
        instance_eval(&block) if block
      end
      def grant(options = {})
        @classPathElement.grantPermission(options[:perm])
      end
    end

    private
    def construct_container
      if @parent && !@caf && !@impl
        container = @parent.makeChildContainer
      else
        args = [classloader, @caf]
        args.delete(nil)
        args << @parent if @parent || args.length == 2
        container = @impl.new(*args)
        @parent.addChildContainer(container.pico) if @parent
      end
      container.changeMonitor(@monitor) if @monitor && container.respond_to?(:changeMonitor)
      container
    end

    def classloader
      @parent && @parent.getComponentClassLoader || $parent && $parent.getComponentClassLoader
    end

  end

  module Builder
    def container(options = {}, &block)
      Container.new(options).build(&block)
    end
  end
end

class Object
  include Nano::Builder
end
