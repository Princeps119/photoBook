<script lang="ts">
	import { goto } from "$app/navigation";
    let username = '';
    let email = '';
    let password = '';
    let confirmPassword = '';
    let error = '';
    let loading = false;

    const handleSubmit = async (e: Event) => {
        e.preventDefault();
        loading = true;
        error = '';
        
        if (!username || !email || !password || !confirmPassword) {
            error = 'Bitte fülle alle Felder aus';
            return;
        }
        
        if (password !== confirmPassword) {
            error = 'Passwörter stimmen nicht überein';
            return;
        }
        
        if (password.length < 6) {
            error = 'Das Passwort muss mindestens 6 Zeichen lang sein';
            return;
        }
        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, email, password })
            });
            
            const data = await response.json();
            
            if (response.ok) {
                goto('/login');
            } else {
                error = data.message || 'Registrierung fehlgeschlagen. Bitte versuche es erneut.';
                console.error('Registration failed:', data);
            }
        } catch (err) {
            error = err instanceof Error ? err.message : 'Ein Fehler ist aufgetreten. Bitte versuche es erneut.';
            console.error('Error during registration:', err);
        } finally {
            loading = false;
        }
    };
</script>

<main>
    <div class="register-box">
        <h1>Account erstellen</h1>
        
        {#if error}
            <div class="error-message">{error}</div>
        {/if}
        
        <form onsubmit={(event) => handleSubmit(event)} autocomplete="off">
            <div class="form-group">
                <label for="username">Benutzername<span class="required">*</span></label>
                <input
                    type="text"
                    id="username"
                    bind:value={username}
                    placeholder="Gib deinen Benutzernamen ein"
                    disabled={loading}
                    autocomplete="off"
                    required
                />
            </div>
            
            <div class="form-group">
                <label for="email">Email<span class="required">*</span></label>
                <input
                    type="email"
                    id="email"
                    bind:value={email}
                    placeholder="Gib deine Email ein"
                    disabled={loading}
                    autocomplete="off"
                    required
                />
            </div>
            
            <div class="form-group">
                <label for="password">Passwort<span class="required">*</span></label>
                <input
                    type="password"
                    id="password"
                    bind:value={password}
                    placeholder="Gib dein Passwort ein"
                    disabled={loading}
                    autocomplete="off"
                    required
                />
            </div>
            
            <div class="form-group">
                <label for="confirmPassword">Passwort bestätigen<span class="required">*</span></label>
                <input
                    type="password"
                    id="confirmPassword"
                    bind:value={confirmPassword}
                    placeholder="Bestätige dein Passwort"
                    disabled={loading}
                    autocomplete="off"
                    required
                />
            </div>
            
            <button type="submit" disabled={loading}>
                {#if loading}
                    Registrieren...
                {:else}
                    Registrieren
                {/if}
            </button>
        </form>
        
        <p class="login-link">
            Schon einen Account? <a href="/login">zum Login</a>
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

    .register-box {
        background: #F59E0B;
        padding: 40px;
        border-radius: 8px;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
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
        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
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

    .login-link {
        text-align: center;
        margin-top: 20px;
        color: #666;
        font-size: 14px;
    }

    .login-link a {
        color: #1E293B;
        text-decoration: none;
        font-weight: 600;
    }

    .login-link a:hover {
        text-decoration: underline;
    }

    .required {
        color: red;
        margin-left: 4px;
    }
</style>


