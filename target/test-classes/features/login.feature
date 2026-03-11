#Author: neha.verma@inadev.com
Feature: InsureCRM Login Validation
  As a InsureCRM user
  I want to login successfully
  So that I can access the home page

  @SMOKE
  Scenario: Validate login with valid credentials
    Given I am on InsureCRM page
    When User enters "admin"
    And User enters password "dsr123!@#aA"
    And User clicks on the login button
    Then User is logged in

  Scenario Outline: Validate login with multiple credentials
    Given I am on InsureCRM page
    When User enters "<username>"
    And User enters password "<password>"
    And User clicks on the login button
    Then User <outcome>

    Examples: 
      | username    | password      | outcome                     |
      | admin       | dsr123!@#aA     | is logged in                |
      | invalidUser | dsr123!@#aA     | should see an alert         |
      | admin       | wrongPassword | should see an error message |
      |             | suitecrm8     | should see an error message |
      | admin       |               | should see an error message |
