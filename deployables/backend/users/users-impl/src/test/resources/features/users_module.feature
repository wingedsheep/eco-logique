Feature: Module Level User Management
  I want to verify user profile management within the module boundary, ensuring all business rules and edge cases are handled correctly.

  Background:
    Given the module is running

  Scenario: Create a new user profile
    Given I am authenticated as "new-user-subject"
    When I create a profile with the following details:
      | name  | John Doe          |
      | email | john@example.com  |
    Then the profile should be created successfully
    And the profile should have name "John Doe"
    And the profile should have email "john@example.com"

  Scenario: Create a user profile with address
    Given I am authenticated as "user-with-address"
    When I create a profile with address:
      | name        | Jane Smith      |
      | email       | jane@example.com |
      | street      | Kalverstraat    |
      | houseNumber | 42              |
      | postalCode  | 1012 NX         |
      | city        | Amsterdam       |
      | countryCode | NETHERLANDS     |
    Then the profile should be created successfully
    And the profile should have an address in "Amsterdam"

  Scenario: Retrieve user profile
    Given I am authenticated as "existing-user-subject"
    And a profile exists for the current user with name "Existing User"
    When I retrieve my profile
    Then I should receive the profile details
    And the profile name should be "Existing User"

  Scenario: Update user address
    Given I am authenticated as "address-update-subject"
    And a profile exists for the current user with name "Address Updater"
    When I update my address to:
      | street      | Alexanderplatz |
      | houseNumber | 1              |
      | postalCode  | 10178          |
      | city        | Berlin         |
      | countryCode | GERMANY        |
    Then the address should be updated successfully
    And the profile should have an address in "Berlin"

  Scenario: Cannot create duplicate profile
    Given I am authenticated as "duplicate-user-subject"
    And a profile exists for the current user with name "Original User"
    When I try to create another profile
    Then I should receive an already exists error

  Scenario: Cannot use duplicate email
    Given I am authenticated as "first-email-user"
    And a profile exists for the current user with email "taken@example.com"
    When I am authenticated as "second-email-user"
    And I try to create a profile with email "taken@example.com"
    Then I should receive an email already exists error

  Scenario: Cannot create profile with invalid email
    Given I am authenticated as "invalid-email-user"
    When I try to create a profile with email "not-an-email"
    Then I should receive a validation error

  Scenario: Cannot create profile with invalid country
    Given I am authenticated as "invalid-country-user"
    When I try to create a profile with country "INVALID_COUNTRY"
    Then I should receive an invalid country error

  Scenario: Cannot retrieve non-existent profile
    Given I am authenticated as "non-existent-user"
    When I retrieve my profile
    Then I should receive a not found error

  Scenario: Cannot update address for non-existent profile
    Given I am authenticated as "no-profile-user"
    When I update my address to:
      | street      | Test Street |
      | houseNumber | 1           |
      | postalCode  | 12345       |
      | city        | Test City   |
      | countryCode | NETHERLANDS |
    Then I should receive a not found error
