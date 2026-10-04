export interface Profile {
  userId: number;
  name: string;
  email: string;
  createdAt: string;
}

export function profileFromResponse(response: unknown): Profile {
  if (
    typeof response !== 'object' ||
    response === null ||
    !('userId' in response) ||
    typeof response.userId !== 'number' ||
    !('name' in response) ||
    typeof response.name !== 'string' ||
    !('email' in response) ||
    typeof response.email !== 'string' ||
    !('createdAt' in response) ||
    typeof response.createdAt !== 'string'
  ) {
    throw new Error('The API returned an invalid profile.');
  }

  return {
    userId: response.userId,
    name: response.name,
    email: response.email,
    createdAt: response.createdAt,
  };
}
