const mongoose = require("mongoose");

const goalSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
    },
    title: {
      type: String,
      required: [true, "Please add a goal title"],
      trim: true,
      maxlength: [100, "Goal title cannot exceed 100 characters"],
    },
    description: {
      type: String,
      required: [true, "Please add a goal description"],
      trim: true,
      maxlength: [500, "Goal description cannot exceed 500 characters"],
    },
    category: {
      type: String,
      required: [true, "Please specify a category"],
      enum: ["HEALTH", "CAREER", "EDUCATION", "PERSONAL", "FINANCE", "OTHER"],
    },
    difficulty: {
      type: Number,
      required: [true, "Please specify a difficulty level"],
      min: 1,
      max: 5,
    },
    deadline: {
      type: Date,
      required: false,
    },
    status: {
      type: String,
      enum: ["ACTIVE", "COMPLETED", "ABANDONED"],
      default: "ACTIVE",
    },
    progress: {
      type: Number,
      default: 0,
      min: 0,
      max: 100,
    },
    relatedQuestIds: [
      {
        type: mongoose.Schema.Types.ObjectId,
        ref: "Progress.taskHistory",
      },
    ],
    createdAt: {
      type: Date,
      default: Date.now,
    },
    completedAt: {
      type: Date,
    },
  },
  {
    timestamps: true,
  }
);

// Create indexes for faster queries
goalSchema.index({ user: 1 });
goalSchema.index({ status: 1 });

const Goal = mongoose.model("Goal", goalSchema);

module.exports = Goal;
