<script lang="ts">
    import { goto, invalidateAll } from '$app/navigation';
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
            const response = await api.Auth.login(email, password);
            if (response.success === true) {
                if (response.username) {
                    localStorage.setItem('username', response.username);
                }
                goto('/', { invalidateAll: true });
            }

        } catch (err) {
            error = err instanceof Error ? err.message : 'Login failed. Please try again.';
        } finally {
            isLoading = false;
        }
    }

    function navigateToRegister() {
        goto('/register');
    }
</script>

<main>
    <div class="login-box">

        <h1>Login</h1>

        {#if error}
            <div class="error-message">{error}</div>
        {/if}

        <form onsubmit={handleLogin} autocomplete="off">

            <div class="form-group">
                <label for="email">Email</label>
                <input
                    type="email"
                    id="email"
                    bind:value={email}
                    placeholder="Gib deine Email ein"
                    disabled={isLoading}
                    required
                />
            </div>

            <div class="form-group">
                <label for="password">Passwort</label>
                <input
                    type="password"
                    id="password"
                    bind:value={password}
                    placeholder="Gib dein Passwort ein"
                    disabled={isLoading}
                    required
                />
            </div>

            <button type="submit" disabled={isLoading || !email || !password}>
                {#if isLoading}
                    Einloggen...
                {:else}
                    Login
                {/if}
            </button>

        </form>

        <p class="register-link">
            Noch keinen Account? 
            <a href="/register">Registrieren</a>
        </p>

    </div>
</main>

<style>

main {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 100vh;
    padding: 20px;
}

.login-box {
    background: #F59E0B;
    padding: 40px;
    border-radius: 8px;
    box-shadow: 0 10px 25px rgba(0,0,0,0.2);
    width: 100%;
    max-width: 400px;
}

h1 {
    text-align: center;
    color: #1E293B;
    margin-top: 0;
    margin-bottom: 30px;
    font-size: 28px;
}

.error-message {
    background-color: #fee;
    color: #c33;
    padding: 12px;
    border-radius: 4px;
    margin-bottom: 20px;
    font-size: 14px;
    border: 1px solid #fcc;
}

.form-group {
    margin-bottom: 20px;
    display: flex;
    flex-direction: column;
}

label {
    color: #1E293B;
    font-weight: 500;
    margin-bottom: 8px;
    font-size: 14px;
}

input {
    padding: 12px;
    border-radius: 4px;
    font-size: 14px;
    background-color: #1E293B;
    color: white;
}

input:focus {
    outline: none;
    box-shadow: 0 0 0 3px rgba(30,41,59,0.2);
}

button {
    width: 100%;
    padding: 12px;
    background: #1E293B;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: 0.3s;
    margin-top: 10px;
}

button:hover:not(:disabled) {
    transform: translateY(-2px);
}

button:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.register-link {
    text-align: center;
    margin-top: 20px;
    color: #666;
    font-size: 14px;
}

.register-link a {
    color: #1E293B;
    text-decoration: none;
    font-weight: 600;
}

.register-link a:hover {
    text-decoration: underline;
}

</style>