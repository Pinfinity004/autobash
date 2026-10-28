# Signing

`app/build.gradle` signs the **debug** variant with a local development key:

```groovy
signingConfigs {
    personal {
        storeFile file("../signing/development.jks")
        storePassword "android"
        keyAlias "imagetap-v7"
        keyPassword "android"
    }
}
```

That key is **deliberately excluded from this repository** — see `.gitignore`. Private keys
do not belong in version control, even throwaway ones, because the same file tends to get
reused out of convenience and then signs something that matters.

It is a development key with the password `android`. It provides no security whatsoever; it
exists so debug builds share a stable signature, which lets you install a new build over the
old one without uninstalling and losing your presets.

## Building without the original key

Either supply your own `signing/development.jks` with the alias and passwords above:

```bash
keytool -genkeypair -v -keystore signing/development.jks \
        -storepass android -keypass android \
        -alias imagetap-v7 -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=AutoBash Dev, O=AutoBash, C=US"
```

or point the build at the Android default debug key by deleting the `signingConfigs` block and
the `signingConfig` line from `app/build.gradle`. Note that changing the signing key means an
in-place upgrade over a previous install will fail with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`;
export your presets first.

## Release builds

For anything you distribute, generate a real keystore kept **outside** this repository, and
load its passwords from environment variables or a `keystore.properties` file that is
gitignored. Never hardcode a release password the way the development one above is
hardcoded.
