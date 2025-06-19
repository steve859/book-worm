// src/utils/error.js

/**
 * Mapping of backend error codes to user-friendly messages.
 * These codes correspond to the ErrorCode enum in the backend.
 */
export const errorMessages = {
  9999: 'Uncategorized error. Please try again later.',
  1001: 'Invalid request. Please check the information provided.',
  1002: 'User already exists. Please choose a different username.',
  1003: 'Username is too short. It must be at least {min} characters.',
  1004: 'Password is too short. It must be at least {min} characters.',
  1005: 'User not found. Please check the username.',
  1006: 'You are not authenticated. Please sign in again.',
  1007: 'You do not have permission to perform this action.',
  1008: 'Invalid date of birth. You must be at least {min} years old.',
  1009: 'Requested import quantity exceeds available stock.',
  1010: 'Requested book not found.',
  1011: 'Author not found.',
  1012: 'Category not found.',
  1013: 'Import receipt not found.',
  1014: 'Invoice not found.',
  1015: 'Payment receipt not found.',
  1016: 'Category already exists.',
  1017: 'Import quantity must be at least 150 books.',
  1018: 'Debt amount limit exceeded.',
  1019: 'Stock level is too low to proceed with export.',
  1020: 'Monthly debt report not found.',
  1021: 'Book title already exists.',
  1022: 'Monthly inventory report not found.',
  1023: 'Monthly inventory report detail not found.',
  1024: 'Cannot delete book because it has import receipts.',
  1025: 'Failed to delete book due to database constraints.'
};

/**
 * Retrieve a user-friendly message for a given error code.
 * @param {number} code - The error code returned from backend
 * @param {object} variables - Optional variables to interpolate into message
 * @returns {string}
 */
export function getErrorMessage(code, variables) {
  let msg = errorMessages[code] || 'An unexpected error occurred. Please try again.';
  // Simple variable interpolation: replace {key} with variables[key]
  if (variables && typeof variables === 'object') {
    Object.keys(variables).forEach(key => {
      const pattern = new RegExp(`\\{${key}\\}`, 'g');
      msg = msg.replace(pattern, variables[key]);
    });
  }
  return msg;
}
