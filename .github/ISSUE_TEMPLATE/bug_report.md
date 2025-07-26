name: Bug report
description: Use this template to report a bug
title: "[Bug]: "
labels: ["bug"]
body:
- type: textarea
  id: description
  attributes:
  label: Description
  description: What happened? Provide steps to reproduce.
  required: true
- type: input
  id: version
  attributes:
  label: Naftah version
  description: Output of `java -jar Naftah.jar --version`
  required: true
- type: textarea
  id: logs
  attributes:
  label: Logs / stack trace
  required: false
