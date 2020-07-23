resource "aws_dynamodb_table" "users" {
  name = "users"
  billing_mode = "PROVISIONED"
  read_capacity = 20
  write_capacity = 20
  hash_key = "githubName"

  attribute {
    name = "githubName"
    type = "S"
  }
}

resource "aws_dynamodb_table" "teams" {
  name = "teams"
  billing_mode = "PROVISIONED"
  read_capacity = 20
  write_capacity = 20
  hash_key = "teamName"

  attribute {
    name = "teamName"
    type = "S"
  }
}

resource "aws_dynamodb_table" "reviews" {
  name = "reviews"
  billing_mode = "PROVISIONED"
  read_capacity = 20
  write_capacity = 20
  hash_key = "githubName"

  attribute {
    name = "githubName"
    type = "S"
  }
}

resource "aws_dynamodb_table" "review_requests" {
  name = "review_requests"
  billing_mode = "PROVISIONED"
  read_capacity = 20
  write_capacity = 20
  hash_key = "htmlUrl"

  attribute {
    name = "htmlUrl"
    type = "S"
  }
}

