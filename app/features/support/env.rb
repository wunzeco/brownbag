require 'rest-client'

class DataLoader

  attr_reader :response_prepare, :response_load

  def initialize(host, port)
    @host = host
    @port = port
  end

  def index()
    RestClient.get("http://#{@host}:#{@port}")
  end

  def prepare()
    @response_prepare = RestClient.get("http://#{@host}:#{@port}/prepare")
    @response_prepare
  end

  def load()
    @response_load = RestClient.post("http://#{@host}:#{@port}/load", data="")
    @response_load
  end
end


