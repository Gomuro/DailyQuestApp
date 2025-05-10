const Goal = require("../models/goalModel");
const Progress = require("../models/progressModel");

/**
 * Create a new goal for a user
 * @param {string} userId - User ID
 * @param {Object} goalData - Goal data
 * @returns {Promise<Object>} Created goal
 */
const createGoal = async (userId, goalData) => {
  const goal = await Goal.create({
    user: userId,
    ...goalData,
  });

  return goal;
};

/**
 * Update an existing goal
 * @param {string} goalId - Goal ID
 * @param {string} userId - User ID (for validation)
 * @param {Object} updateData - Goal data to update
 * @returns {Promise<Object>} Updated goal
 */
const updateGoal = async (goalId, userId, updateData) => {
  const goal = await Goal.findById(goalId);

  if (!goal) {
    throw new Error("Goal not found");
  }

  // Ensure the goal belongs to the user
  if (goal.user.toString() !== userId.toString()) {
    throw new Error("Not authorized to update this goal");
  }

  // If marking as completed, set the completedAt date
  if (updateData.status === "COMPLETED" && goal.status !== "COMPLETED") {
    updateData.completedAt = new Date();
  }

  const updatedGoal = await Goal.findByIdAndUpdate(goalId, updateData, {
    new: true,
    runValidators: true,
  });

  return updatedGoal;
};

/**
 * Get all goals for a user
 * @param {string} userId - User ID
 * @param {string} status - Optional status filter
 * @returns {Promise<Array>} List of goals
 */
const getGoals = async (userId, status) => {
  const query = { user: userId };

  if (status) {
    query.status = status;
  }

  const goals = await Goal.find(query).sort({ createdAt: -1 });
  return goals;
};

/**
 * Get a single goal by ID
 * @param {string} goalId - Goal ID
 * @param {string} userId - User ID (for validation)
 * @returns {Promise<Object>} Goal object
 */
const getGoalById = async (goalId, userId) => {
  const goal = await Goal.findById(goalId);

  if (!goal) {
    throw new Error("Goal not found");
  }

  // Ensure the goal belongs to the user
  if (goal.user.toString() !== userId.toString()) {
    throw new Error("Not authorized to access this goal");
  }

  return goal;
};

/**
 * Delete a goal
 * @param {string} goalId - Goal ID
 * @param {string} userId - User ID (for validation)
 * @returns {Promise<Object>} Deletion result
 */
const deleteGoal = async (goalId, userId) => {
  const goal = await Goal.findById(goalId);

  if (!goal) {
    throw new Error("Goal not found");
  }

  // Ensure the goal belongs to the user
  if (goal.user.toString() !== userId.toString()) {
    throw new Error("Not authorized to delete this goal");
  }

  await goal.deleteOne();

  return { success: true, message: "Goal deleted" };
};

/**
 * Update goal progress based on completed quest
 * @param {string} userId - User ID
 * @param {string} goalId - Goal ID
 * @param {number} progressIncrement - Progress increment (0-100)
 * @param {string} questId - Quest ID to add to related quests
 * @returns {Promise<Object>} Updated goal
 */
const updateGoalProgress = async (
  userId,
  goalId,
  progressIncrement,
  questId
) => {
  const goal = await Goal.findById(goalId);

  if (!goal) {
    throw new Error("Goal not found");
  }

  // Ensure the goal belongs to the user
  if (goal.user.toString() !== userId.toString()) {
    throw new Error("Not authorized to update this goal's progress");
  }

  // Calculate new progress, ensuring it doesn't exceed 100%
  let newProgress = goal.progress + progressIncrement;
  if (newProgress > 100) newProgress = 100;

  // Update progress and add quest to related quests if provided
  const updateData = { progress: newProgress };

  if (questId) {
    updateData.$addToSet = { relatedQuestIds: questId };
  }

  // If progress reaches 100%, mark goal as completed
  if (newProgress >= 100 && goal.status === "ACTIVE") {
    updateData.status = "COMPLETED";
    updateData.completedAt = new Date();
  }

  const updatedGoal = await Goal.findByIdAndUpdate(goalId, updateData, {
    new: true,
    runValidators: true,
  });

  return updatedGoal;
};

/**
 * Get active goal with most recent activity
 * @param {string} userId - User ID
 * @returns {Promise<Object>} Active goal or null if none exists
 */
const getActiveGoal = async (userId) => {
  const activeGoal = await Goal.findOne({
    user: userId,
    status: "ACTIVE",
  }).sort({ updatedAt: -1 });

  return activeGoal;
};

module.exports = {
  createGoal,
  updateGoal,
  getGoals,
  getGoalById,
  deleteGoal,
  updateGoalProgress,
  getActiveGoal,
};
