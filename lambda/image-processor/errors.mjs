export class PermanentImageError extends Error {
  constructor(message, options) {
    super(message, options);
    this.name = 'PermanentImageError';
  }
}
