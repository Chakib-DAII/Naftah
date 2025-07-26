name: Feature request
description: Suggest a new feature or enhancement
title: "[Feature]: "
labels: ["enhancement"]
body:
- type: input
  id: summary
  attributes:
  label: Title
  description: One‑line summary of the feature
  required: true
- type: textarea
  id: motivation
  attributes:
  label: Motivation
  description: Why is this feature useful?
  required: true
- type: textarea
  id: design
  attributes:
  label: Potential design
  description: Ideas or possible implementation
  required: false
