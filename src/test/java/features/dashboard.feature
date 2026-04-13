#Author: neha.verma@inadev.com
Feature: Verify Login and Logout Functionality with Valid Credentials
  As a valid InsureCRM user
  I want to log in, access the dashboard, and log out successfully
  So that the application session flow is validated securely

  @SMOKE
  Scenario: Verify login and logout functionality with valid credentials
    Given I am on InsureCRM page
    When User enters "admin"
    And User enters password "dsr123!@#aA"
    And User clicks on the login button
    Then User is logged in
    And User should see the dashboard page
    And User should see the welcome message
    And User should see the dashboard UI elements
    And User should see the user profile information displayed
    When User clicks on the user profile icon
    And User clicks on the logout button
    Then User should be redirected to login page
    And Logout message should be displayed
    When User uses browser back button
    Then User should not be able to access the dashboard without re-authentication
    And User should be redirected to login page automatically
    
  @REGRESSION
  Scenario: Validate user can logout from dashboard with session termination
    Given I am on InsureCRM page
    When User enters "admin"
    And User enters password "dsr123!@#aA"
    And User clicks on the login button
    Then User is logged in
    When User clicks on the user profile menu
    And User clicks on the logout button
    Then User should be redirected to login page
    And Logout message should be displayed
    When User uses browser back button
    Then User should not be able to access the dashboard without re-authentication
    And User should be redirected to login page automatically

  @REGRESSION
  Scenario Outline: Validate dashboard elements visibility
    Given I am on InsureCRM page
    When User enters "<username>"
    And User enters password "<password>"
    And User clicks on the login button
    Then User is logged in
    And User should see "<element>" on dashboard

    Examples:
      | username | password      | element           |
      | admin    | dsr123!@#aA   | sidebar           |
      | admin    | dsr123!@#aA   | header            |
      | admin    | dsr123!@#aA   | user profile icon |
