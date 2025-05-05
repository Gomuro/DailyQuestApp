const dotenv = require("dotenv");
const path = require("path");

// Load environment variables from .env file
dotenv.config({ path: path.resolve(__dirname, "../../.env") });

module.exports = {
  NODE_ENV: process.env.NODE_ENV || "development",
  PORT: process.env.PORT || 5000,
  MONGO_URI: process.env.MONGO_URI || "mongodb://localhost:27017/dailyquest",
  JWT_SECRET: process.env.JWT_SECRET || "dev_jwt_secret",
  JWT_EXPIRE: process.env.JWT_EXPIRE || "30d",
};
