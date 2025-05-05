const asyncHandler = require("express-async-handler");
const progressService = require("../services/progressService");

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

  const result = await progressService.saveTaskHistory(userId, {
    quest,
    points,
    status,
  });

  res.status(200).json(result);
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
