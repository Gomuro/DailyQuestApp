const mongoose = require("mongoose");

// Define a schema for task history entries
const taskSchema = new mongoose.Schema({
  quest: {
    type: String,
    required: true,
  },
  points: {
    type: Number,
    required: true,
  },
  status: {
    type: String,
    enum: ["COMPLETED", "REJECTED"],
    required: true,
  },
  timestamp: {
    type: Date,
    default: Date.now,
  },
});

// Main progress schema
const progressSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
      unique: true,
    },
    currentSeed: {
      type: Number,
      default: 0,
    },
    seedDay: {
      type: Number,
      default: -1,
    },
    rejectCount: {
      type: Number,
      default: 0,
    },
    lastRejectDay: {
      type: Number,
      default: -1,
    },
    taskHistory: [taskSchema],
    themePreference: {
      type: Number,
      default: 2, // System (2) is default, Light (0), Dark (1)
    },
  },
  {
    timestamps: true,
  }
);

// Create indexes for faster queries
progressSchema.index({ user: 1 });

const Progress = mongoose.model("Progress", progressSchema);

module.exports = Progress;
