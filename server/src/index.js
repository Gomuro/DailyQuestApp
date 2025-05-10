const express = require("express");
const cors = require("cors");
const morgan = require("morgan");
const connectDB = require("./config/db");
const config = require("./config/config");
const { errorHandler, notFound } = require("./middlewares/errorMiddleware");

// Routes
const authRoutes = require("./routes/authRoutes");
const progressRoutes = require("./routes/progressRoutes");
const goalRoutes = require("./routes/goalRoutes");

// Initialize Express app
const app = express();

// Connect to MongoDB
connectDB();

// Middleware
app.use(cors());
app.use(express.json());

// Logging middleware in development
if (config.NODE_ENV === "development") {
  app.use(morgan("dev"));
}

// Mount routes
app.use("/api/auth", authRoutes);
app.use("/api/progress", progressRoutes);
app.use("/api/goals", goalRoutes);

// Simple health check route
app.get("/", (req, res) => {
  res.json({
    message: "API is running",
    version: "1.0.0",
    status: "ok",
  });
});

// Error handling middleware
app.use(notFound);
app.use(errorHandler);

// Start server
const PORT = config.PORT;
app.listen(PORT, () => {
  console.log(`Server running in ${config.NODE_ENV} mode on port ${PORT}`);
});

// For testing purposes
module.exports = app;
