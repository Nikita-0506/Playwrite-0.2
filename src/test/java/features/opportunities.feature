#Author: neha.verma@inadev.com
Feature: InsureCRM Opportunities Validation
  As a InsureCRM user
  I want to create opportunities successfully
  So that I can track potential deals

  @OPPORTUNITIES
  Scenario: Create a new opportunity with valid mandatory fields
    Given I am on InsureCRM page
    When User enters "admin"
    And User enters password "dsr123!@#aA"
    And User clicks on the login button
    Then User is logged in
    When User navigates to Opportunities module
    And User clicks on Create Opportunity
    And User enters opportunity name "Auto Deal 001"
    And User selects account name "admin"
    And User enters expected close date "30/04/2026"
    And User enters opportunity amount "50000"
    And User selects opportunity stage "Prospecting"
    And User saves the opportunity