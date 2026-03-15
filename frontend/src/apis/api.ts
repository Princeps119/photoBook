const BASE_URL = 'http://localhost:8080';

interface RequestOptions {
  method?: string;
  headers?: Record<string, string>;
  body?: any;
}

async function request(endpoint: string, options: RequestOptions = {}): Promise<any> {
  console.log("API.ts")
  const url = `${endpoint}`;
  const defaultHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
  };

  const response = await fetch(url, {
    method: options.method || 'GET',
    headers: { ...defaultHeaders, ...options.headers },
    body: options.body ? JSON.stringify(options.body) : undefined,
  });
  console.log(response)

  if (!response.ok) {
    throw new Error(`API Error: ${response.status} ${response.statusText}`);
  }

  return response.json();
}

export const api = {
  Photos: {
    get: (id: string) =>
      request(`/api/photos/${id}`),
    post: async (
      file: File,
      title: string,
      description: string,
      type: 'Public' | 'Private',
      userEmail: string,
    ) => {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('title', title);
      // formData.append('description', description);
      formData.append('type', type);
      // formData.append('userEmail', userEmail);

      const response = await fetch('/api/upload', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error(`API Error: ${response.status} ${response.statusText}`);
      }

      return response.json();
    },
    put: (id: string, data: any) =>
      request(`/photos/${id}`, { method: 'PUT', body: data }),
    delete: (id: string) =>
      request(`/photos/${id}`, { method: 'DELETE' }),
  },

  Users: {
    get: (id?: string) =>
      request(`/users${id ? `/${id}` : ''}`),
    post: (data: any) =>
      request('/users', { method: 'POST', body: data }),
    put: (id: string, data: any) =>
      request(`/users/${id}`, { method: 'PUT', body: data }),
    delete: (id: string) =>
      request(`/users/${id}`, { method: 'DELETE' }),
  },

  Auth: {
    login: (mail: string, password: string) =>
      request(`/api/auth/login`, { method: 'POST', body: { mail, password } }),
    register: (username: string, email: string, password: string) =>
      request('/api/auth/register', { method: 'POST', body: { username, email, password } }),
    logout: () =>
      request('/api/auth/logout', { method: 'POST' }),
  },
};