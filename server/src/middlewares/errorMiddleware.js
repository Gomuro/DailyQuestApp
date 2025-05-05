const config = require("../config/config");

/**
 * Error handler middleware
 */
const errorHandler = (err, req, res, next) => {
  // Set status code based on error or default to 500
  const statusCode = err.statusCode ? err.statusCode : 500;

  // Create error response
  const errorResponse = {
    message: err.message,
    stack: config.NODE_ENV === "production" ? null : err.stack,
  };

  res.status(statusCode).json(errorResponse);
};

/**
 * 404 Not Found middleware
 */
const notFound = (req, res, next) => {
  const error = new Error(`Not Found - ${req.originalUrl}`);
  res.status(404);
  next(error);
};

module.exports = {
  errorHandler,
  notFound,
};
