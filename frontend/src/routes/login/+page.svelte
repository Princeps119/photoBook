<script lang="ts">
    import { goto } from '$app/navigation';
    import { api } from "../../apis/api";

    let email: string = $state('');
    let password: string = $state('');
    let error: string = $state('');
    let isLoading: boolean = $state(false);

    async function handleLogin(e: SubmitEvent) {
        e.preventDefault();
        error = '';
        isLoading = true;

        try {
            const response= await api.Auth.login(email, password);
            
            // Store the token in localStorage or a cookie
            // if (response.token) {
            //     localStorage.setItem('authToken', response.token);
            //     localStorage.setItem('userEmail', email);
            // }
            console.log('Login Response:', response);

            if (response.success == true) {
                if (response.username) {
                    localStorage.setItem('username', response.username);
                }
                 goto('/upload');
                
            }

        } catch (err) {
            error = err instanceof Error ? err.message : 'Login failed. Please try again.';
            console.error('Login error:', err);
        } finally {
            isLoading = false;
        }
    }

    function navigateToRegister() {
        goto('/register');
    }
</script>

<div class="flex min-h-screen items-center justify-center px-4">
    <div class="w-full max-w-md">
        <!-- Login Card -->
        <div class="rounded-lg bg-white shadow-2xl">
            <!-- Header -->
            <div class="bg-gradient-to-r from-blue-600 to-blue-800 px-6 py-8">
                <h1 class="text-3xl font-bold text-white">Login</h1>
                <p class="mt-2 text-blue-100">Welcome to the PhotoBook</p>
            </div>

            <!-- Form Content -->
            <div class="p-6">
                <!-- Error Message -->
                {#if error}
                    <div class="mb-4 rounded-lg bg-red-50 p-4 text-sm text-red-800">
                        <p class="font-semibold">Login Failed</p>
                        <p>{error}</p>
                    </div>
                {/if}

                <!-- Login Form -->
                <form onsubmit={handleLogin} class="space-y-4">
                    <!-- Email Field -->
                    <div>
                        <label for="email" class="block text-sm font-semibold text-gray-700">
                            Email Address
                        </label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            required
                            bind:value={email}
                            placeholder="you@example.com"
                            disabled={isLoading}
                            class="mt-2 w-full rounded-lg border border-gray-300 px-4 py-2 text-gray-900 placeholder-gray-400 transition focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200 disabled:cursor-not-allowed disabled:bg-gray-100"
                        />
                    </div>

                    <!-- Password Field -->
                    <div>
                        <label for="password" class="block text-sm font-semibold text-gray-700">
                            Password
                        </label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            required
                            bind:value={password}
                            placeholder="••••••••"
                            disabled={isLoading}
                            class="mt-2 w-full rounded-lg border border-gray-300 px-4 py-2 text-gray-900 placeholder-gray-400 transition focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200 disabled:cursor-not-allowed disabled:bg-gray-100"
                        />
                    </div>

                    <!-- Submit Button -->
                    <button
                        type="submit"
                        disabled={isLoading || !email || !password}
                        class="mt-6 w-full rounded-lg bg-gradient-to-r from-blue-600 to-blue-700 py-2 font-semibold text-white transition hover:from-blue-700 hover:to-blue-800 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                        {#if isLoading}
                            <span class="flex items-center justify-center">
                                <svg
                                    class="mr-2 h-4 w-4 animate-spin"
                                    xmlns="http://www.w3.org/2000/svg"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                >
                                    <circle
                                        class="opacity-25"
                                        cx="12"
                                        cy="12"
                                        r="10"
                                        stroke="currentColor"
                                        stroke-width="4"
                                    ></circle>
                                    <path
                                        class="opacity-75"
                                        fill="currentColor"
                                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                                    ></path>
                                </svg>
                                Logging in...
                            </span>
                        {:else}
                            Login
                        {/if}
                    </button>
                </form>

                <!-- Divider -->
                <div class="my-6 flex items-center">
                    <div class="flex-1 border-t border-gray-300"></div>
                    <span class="px-3 text-sm text-gray-600">Or</span>
                    <div class="flex-1 border-t border-gray-300"></div>
                </div>

                <!-- Register Link -->
                <button
                    type="button"
                    onclick={navigateToRegister}
                    class="w-full rounded-lg border-2 border-blue-600 py-2 font-semibold text-blue-600 transition hover:bg-blue-50"
                >
                    Create a new account
                </button>
            </div>

            <!-- Footer -->
            <div class="border-t border-gray-200 bg-gray-50 px-6 py-4 text-center text-xs text-gray-600">
                <p>Don't have an account? <span class="cursor-pointer font-semibold text-blue-600">Sign up here</span></p>
            </div>
        </div>

        <!-- Help Text -->
        <p class="mt-4 text-center text-sm text-gray-300">
            Need help? Contact support@photobook.com
        </p>
    </div>
</div>

<style>
    /* Add any additional custom styles here */
</style>