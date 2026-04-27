#Author: neha.verma@inadev.com
Feature: InsureCRM Templates Filter Validation
  As a InsureCRM user
  I want to filter templates by keyword
  So that I can quickly find the required template from the list

  @TEMPLATES
  Scenario: Filter Templates by keyword and validate filtered results
    Given I am on InsureCRM page
    When User enters "admin"
    And User enters password "dsr123!@#aA"
    And User clicks on the login button
    Then User is logged in
    And User navigates to Templates module from More menu
    Then Templates list page should be displayed
    When User clicks on Filter button on Templates page
    And User enters filter keyword "Case" in Basic Filter Name field
    And User applies the filter
    Then User should see only templates matching keyword "Case"
    When User clears the filter
    Then User should see the full templates list