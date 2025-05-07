const asyncHandler = require("express-async-handler");
const progressService = require("../services/progressService");
const User = require("../models/userModel");

// Helper function to get today's day of year (1-366)
const getTodayDayOfYear = () => {
  const now = new Date();
  const start = new Date(now.getFullYear(), 0, 0);
  const diff = now - start;
  const oneDay = 1000 * 60 * 60 * 24;
  return Math.floor(diff / oneDay);
};

/**
 * @desc    Save user progress
 * @route   POST /api/progress
 * @access  Private
 */
const saveProgress = asyncHandler(async (req, res) => {
  const userId = req.user._id;
  const { points, streak, lastDay } = req.body;

  const progress = await progressService.saveProgress(userId, {
    points,
    streak,
    lastDay,
  });

  res.status(200).json(progress);
});

/**
 * @desc    Save seed value
 * @route   POST /api/progress/seed
 * @access  Private
 */
const saveSeed = asyncHandler(async (req, res) => {
  const userId = req.user._id;
  const { seed, day } = req.body;

  const result = await progressService.saveSeed(userId, seed, day);
  res.status(200).json(result);
});

/**
 * @desc    Save task history
 * @route   POST /api/progress/task-history
 * @access  Private
 */
const saveTaskHistory = asyncHandler(async (req, res) => {
  const userId = req.user._id;
  const { quest, points, status } = req.body;

  console.log(`[saveTaskHistory] Received request for user ${userId}:`, {
    quest,
    points,
    status,
  });

  const result = await progressService.saveTaskHistory(userId, {
    quest,
    points,
    status,
  });

  console.log(`[saveTaskHistory] Task history saved:`, result);

  if (status === "COMPLETED") {
    try {
      const user = await User.findById(userId);
      console.log(`[saveTaskHistory] Found user:`, {
        totalPoints: user.totalPoints,
        currentStreak: user.currentStreak,
        lastClaimedDay: user.lastClaimedDay,
      });

      const today = getTodayDayOfYear();
      console.log(`[saveTaskHistory] Today's day of year:`, today);

      let newStreak = 1;
      if (user.lastClaimedDay === today - 1) {
        newStreak = user.currentStreak + 1;
      }
      console.log(`[saveTaskHistory] New streak calculated:`, newStreak);

      const oldPoints = user.totalPoints;
      user.totalPoints += points;
      user.currentStreak = newStreak;
      user.lastClaimedDay = today;

      await user.save();
      console.log(`[saveTaskHistory] User updated:`, {
        oldPoints,
        newPoints: user.totalPoints,
        newStreak: user.currentStreak,
        newLastClaimedDay: user.lastClaimedDay,
      });
    } catch (error) {
      console.error(`[saveTaskHistory] Error updating user:`, error);
      throw error;
    }
  }

  res.status(200).json({ message: "Task history saved successfully" });
});

/**
 * @desc    Get task history
 * @route   GET /api/progress/task-history
 * @access  Private
 */
const getTaskHistory = asyncHandler(async (req, res) => {
  const userId = req.user._id;

  const taskHistory = await progressService.getTaskHistory(userId);
  res.status(200).json(taskHistory);
});

/**
 * @desc    Clear task history
 * @route   DELETE /api/progress/task-history
 * @access  Private
 */
const clearTaskHistory = asyncHandler(async (req, res) => {
  const userId = req.user._id;

  const result = await progressService.clearTaskHistory(userId);
  res.status(200).json(result);
});

/**
 * @desc    Update reject info
 * @route   POST /api/progress/reject-info
 * @access  Private
 */
const updateRejectInfo = asyncHandler(async (req, res) => {
  const userId = req.user._id;
  const { count, day } = req.body;

  const result = await progressService.updateRejectInfo(userId, count, day);
  res.status(200).json(result);
});

/**
 * @desc    Save theme preference
 * @route   POST /api/progress/theme
 * @access  Private
 */
const saveThemePreference = asyncHandler(async (req, res) => {
  const userId = req.user._id;
  const { themeMode } = req.body;

  const result = await progressService.saveThemePreference(userId, themeMode);
  res.status(200).json(result);
});

/**
 * @desc    Get theme preference
 * @route   GET /api/progress/theme
 * @access  Private
 */
const getThemePreference = asyncHandler(async (req, res) => {
  const userId = req.user._id;

  const result = await progressService.getThemePreference(userId);
  res.status(200).json(result);
});

module.exports = {
  saveProgress,
  saveSeed,
  saveTaskHistory,
  getTaskHistory,
  clearTaskHistory,
  updateRejectInfo,
  saveThemePreference,
  getThemePreference,
};
