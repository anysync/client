# AnySync Client 

An open source client for [AnySync](https://anysync.net), which is 
a secure cloud storage service with end-to-end encryption.

## Installation and Build

[Go](https://golang.org/dl/) 1.11 or later version is required for build.

After git cloning: 
      
`git clone https://github.com/anysync/client`

run build.sh script to compile, then run

`Sync -help`

to see usage examples.

## Usage 

Suppose you want to sync a folder '~/AnySync'. You can follow these steps:

### - Register your email

`Sync -u=user@example.com register`

An email will be sent to user@example.com, then click the activation link in the email
to get this email ready for creating an AnySync account.

### - Sign up

After clicking the activation link, now you can sign up using the email:

`Sync -u=user@example.com -p=mySecretPasswd -dir="~/AnySync" init`

This command will create an AnySync account and sync folder "~/AnySync".
The newly created account is a trial account with 50GB cloud storage. You can upgrade later.

###### Technical Details
- Secure random 256-bit file key and auth key are generated.
- A [NaCl](https://en.wikipedia.org/wiki/NaCl_(software)) box public/private key pair is generated.
- In the login request, client sends out a data structure with these data:
-- Client version number.
-- Email as user name.
-- Newly generated box public key used by the server for encrypting access token.
- In the login response, it contains following data
-- User ID
-- Device ID
-- Access token encrypted by the box public key. Local box private key is used to decrypt it. The access token will be used on the client side for authentication.

[scrypt](https://en.wikipedia.org/wiki/Scrypt "scrypt") is used for generating key from user's password. Default scrypt parameters are 

`Params{N: 16384, R: 8, P: 1, SaltLen: 16, DKLen: 32}`

The key is used for encrypting the file key, auth key, access token and public/private key pair, and the encrypted data is saved to a local file called "master.keys", which will be sent to the server. In the future, user can use the password to decrypt the file and know all the keys so that all the user's cloud files can be decrypted.

The access token is saved to a local file called "access.keys", which is unecrypted. This file is for authenticating the user, similar to the private key file used by SSH client for passwordless login.

All files will be encrypted by the key using [AES-GCM](https://en.wikipedia.org/wiki/Galois/Counter_Mode "AES-GCM"), then encrypted file will be uploaded to the cloud.

### - Check status

`Sync status`

Check the account type and status

### - Upgrade account (optional)

`Sync buy`

You can then upgrade the account in web browser.

### - Sync

`Sync`

You can create a script to run Sync command regularly. It will sync the folder "~/AnySync" with the cloud.

### - Restore (optional)

`Sync -u=user@example.com -p=mySecretPasswd -dir="~/data" restore` 

Restore all data on the cloud to your local "~/data" folder.

