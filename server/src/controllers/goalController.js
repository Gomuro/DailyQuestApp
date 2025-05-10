const asyncHandler = require("express-async-handler");
const goalService = require("../services/goalService");

/**
 * @desc    Create a new goal
 * @route   POST /api/goals
 * @access  Private
 */
const createGoal = asyncHandler(async (req, res) => {
  const userId = req.user._id;
  const { title, description, category, difficulty, deadline } = req.body;

  const goal = await goalService.createGoal(userId, {
    title,
    description,
    category,
    difficulty,
    deadline: deadline ? new Date(deadline) : undefined,
  });

  res.status(201).json(goal);
});

/**
 * @desc    Get all goals for the logged in user
 * @route   GET /api/goals
 * @access  Private
 */
const getGoals = asyncHandler(async (req, res) => {
  const userId = req.user._id;
  const { status } = req.query;

  const goals = await goalService.getGoals(userId, status);
  res.status(200).json(goals);
});

/**
 * @desc    Get a single goal
 * @route   GET /api/goals/:id
 * @access  Private
 */
const getGoalById = asyncHandler(async (req, res) => {
  const userId = req.user._id;
  const goalId = req.params.id;

  const goal = await goalService.getGoalById(goalId, userId);
  res.status(200).json(goal);
});

/**
 * @desc    Update a goal
 * @route   PUT /api/goals/:id
 * @access  Private
 */
const updateGoal = asyncHandler(async (req, res) => {
  const userId = req.user._id;
  const goalId = req.params.id;
  const updateData = req.body;

  // Handle date conversion for deadline if provided
  if (updateData.deadline) {
    updateData.deadline = new Date(updateData.deadline);
  }

  const updatedGoal = await goalService.updateGoal(goalId, userId, updateData);
  res.status(200).json(updatedGoal);
});

/**
 * @desc    Delete a goal
 * @route   DELETE /api/goals/:id
 * @access  Private
 */
const deleteGoal = asyncHandler(async (req, res) => {
  const userId = req.user._id;
  const goalId = req.params.id;

  const result = await goalService.deleteGoal(goalId, userId);
  res.status(200).json(result);
});

/**
 * @desc    Update goal progress
 * @route   PATCH /api/goals/:id/progress
 * @access  Private
 */
const updateGoalProgress = asyncHandler(async (req, res) => {
  const userId = req.user._id;
  const goalId = req.params.id;
  const { progressIncrement, questId } = req.body;

  if (typeof progressIncrement !== "number" || progressIncrement < 0) {
    res.status(400);
    throw new Error("Progress increment must be a positive number");
  }

  const updatedGoal = await goalService.updateGoalProgress(
    userId,
    goalId,
    progressIncrement,
    questId
  );

  res.status(200).json(updatedGoal);
});

/**
 * @desc    Get active goal
 * @route   GET /api/goals/active
 * @access  Private
 */
const getActiveGoal = asyncHandler(async (req, res) => {
  const userId = req.user._id;

  const activeGoal = await goalService.getActiveGoal(userId);

  if (!activeGoal) {
    return res.status(404).json({ message: "No active goal found" });
  }

  res.status(200).json(activeGoal);
});

module.exports = {
  createGoal,
  getGoals,
  getGoalById,
  updateGoal,
  deleteGoal,
  updateGoalProgress,
  getActiveGoal,
};
