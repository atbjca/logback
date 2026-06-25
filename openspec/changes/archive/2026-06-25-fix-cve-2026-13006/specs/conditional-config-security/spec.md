## ADDED Requirements

### Requirement: If condition rejects Unicode escape sequences

The logback configuration processor SHALL reject `<if>` elements whose `condition` attribute contains Java Unicode escape sequences (`\u` or `\U`) after variable substitution, before Janino compiles the expression.

#### Scenario: Unicode escape bypass of new operator is blocked

- **WHEN** a configuration file contains `<if condition='n\u0067w Integer(1).equals(1)'>` and Janino is on the classpath
- **THEN** the condition MUST NOT be compiled or evaluated
- **AND** the processor MUST emit `NEW_OPERATOR_DISALLOWED_MSG` as an error

#### Scenario: Uppercase Unicode escape is blocked

- **WHEN** a configuration file contains `\U` in the `condition` attribute after substitution
- **THEN** the condition MUST NOT be compiled or evaluated
- **AND** the processor MUST emit `NEW_OPERATOR_DISALLOWED_MSG` as an error

### Requirement: If condition rejects new operator literal

The logback configuration processor SHALL reject `<if>` elements whose `condition` attribute contains the `new` operator (detected as the substring `new `) after variable substitution.

#### Scenario: Literal new operator is blocked

- **WHEN** a configuration file contains `<if condition='new Integer(1).equals(1)'>` and Janino is on the classpath
- **THEN** the condition MUST NOT be compiled or evaluated
- **AND** the processor MUST emit `NEW_OPERATOR_DISALLOWED_MSG` as an error

### Requirement: Normal conditional expressions remain allowed

The logback configuration processor SHALL continue to evaluate `<if condition>` expressions that do not contain `new ` or Unicode escape sequences, using the existing Janino-based evaluator.

#### Scenario: Property-based condition works

- **WHEN** a configuration file contains `<if condition='p("key").equals("value")'>` and the property is set
- **THEN** the if-branch MUST be evaluated according to the expression result
- **AND** no NEW_OPERATOR_DISALLOWED error MUST be emitted
