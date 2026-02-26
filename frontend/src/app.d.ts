// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces

interface TokenData {
  username: string;
  encryptedMail: string;
  timestamp: string;
  version: string;
}
declare global {
	namespace App {
		// interface Error {}
		interface Locals {
			token: TokenData | null;
		}
		// interface PageData {}
		// interface PageState {}
		// interface Platform {}
	}
}

export {};
