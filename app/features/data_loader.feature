Feature: Data Loader Test

  Scenario: Prepare elasticsearch for shakespeare data
	Given I can connect to data_loader app
    When  I send a GET request to "/prepare" endpoint
    Then  the response code for prepare endpoint should be 200

  Scenario: Load shakespeare data
	Given I can connect to data_loader app
    When  I send a POST request to "/load" endpoint
    Then  the response code for load endpoint should be 200
	And   the output should be "Shakespeare Play data loaded!"
