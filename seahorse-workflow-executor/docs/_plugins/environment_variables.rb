# Plugin to add environment variables to the `site` object in Liquid templates
module Jekyll
  class EnvironmentVariablesGenerator < Generator
    def generate(site)
      site.config['WORKFLOW_EXECUTOR_VERSION'] = ENV['WORKFLOW_EXECUTOR_VERSION'] || ''
    end
  end
end
