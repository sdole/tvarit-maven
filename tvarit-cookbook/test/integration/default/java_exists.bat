#!/usr/bin/env bats

@test "java binary is found in PATH" {
  run which java
  [ "$status" -eq 0 ]
}