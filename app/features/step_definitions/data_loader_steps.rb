dl_host = ENV['DL_HOST'] || '192.168.99.100'
dl_port = ENV['DL_PORT'] || 9090

dl = DataLoader.new(dl_host, dl_port)


Given(/^I can connect to data_loader app$/) do
  dl.index()
  expect(dl.index.body).to eq 'Shakespeare Play loader!'
end

When(/^I send a GET request to "([^"]*)" endpoint$/) do |endpoint|
  dl.prepare()
  expect(dl.response_prepare.code).to eq 200
end

Then(/^the response code for prepare endpoint should be (\d+)$/) do |status_code|
  expect(dl.response_prepare.code).to eq status_code.to_i
end
Then(/^the response code for load endpoint should be (\d+)$/) do |status_code|
  expect(dl.response_load.code).to eq status_code.to_i
end

When(/^I send a POST request to "([^"]*)" endpoint$/) do |arg1|
  dl.load()
  expect(dl.response_load.code).to eq 200
end

Then(/^the output should be "([^"]*)"$/) do |arg1|
  expect(dl.response_load.body).to eq "Shakespeare Play data loaded!"
end
