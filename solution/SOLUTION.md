# Solution

## Getting to UMIX

### Universal Machine implementation

The `universal-machine` module contains a Kotlin implementation of the Universal Machine specification.

As part of the tests, the `sandmark.umz` program is run and compared to the `sandmark-expected-output.txt`.

### Decrypting the Codex

Running the `codex.umz` program and passing the decryption key gives us access to the CBD Volume 9 and allows
to dump a new program. Here is the output:

```
self-check succeeded!
enter decryption key:
decrypting...
ok
LOADING: 9876543210

 == CBV ARCHIVE ==
    VOLUME ID 9

 Choose a command:

 p) dump UM data
 x) exit
```

Pressing `p` dumps the binary data of a new program.

The `codex-decryptor` module automates the interaction with the codex and dumps the new program in a file
that we'll call `umix.um` (you'll soon see why).

### Running UMIX

Running `umix.um` starts the Universal Machine IX (UMIX) operating system:

```
12:00:00 1/1/19100
Welcome to Universal Machine IX (UMIX).

This machine is a shared resource. Please do not log
in to multiple simultaneous UMIX servers. No game playing
is allowed.

Please log in (use 'guest' for visitor access).
```

We can first log in as a guest by typing `guest`, and we get our first publication:

```
INTRO.LOG=200@999999|35e6f52e9bc951917c73af391e35e1d
```

## UMIX Solutions
### Guest user

#### Hacking passwords

We're then told to read our mail and get information about a potential login hack using a qbasic program.

The email mentions a few commands:

```
cd code
/bin/umodem hack.bas STOP
/bin/qbasic hack.bas
ls /home
./hack.exe howie
```

This mentions 2 interesting programs:

* `umodem`: to write a file using stdin until a sentinel text
* `qbasic`: to compile programs written in a special programming language, such as `hack.bas`

Running the commands mentioned above, we understand that `hack.bas` already exists, and indeed doesn't compile.

We can check out the contents of `hack.bas` using `cat hack.bas`. It looks like the end is broken indeed.
We can fix it by following the last comment's instructions and implementing a new bruteforce that uses numbers after
the dictionary words. Check out [hack_fixed.bas](hack_fixed.bas) for an implementation of this solution.

After copying the fixed contents of `hack_fixed.bas` to the clipboard, we can delete the existing file and recreate a
new one using `umodem`:

```
rm hack.bas
/bin/umodem hack.bas STOP
```

Then paste the copied content + `STOP`.

Our first use of `umodem` gives us another publication:

```
INTRO.UMD=10@999999|7005f80d6cd9b7b837802f1e58b11b8
```

We can then compile it successfully with `/bin/qbasic hack.bas`.

Let's run it to try and find passwords from users in `/home`:

```
ftd -> falderal90
ohmega -> bidirectional
howie -> xyzzy
```

Let's try to log in to those accounts (see next sections).

#### Extra email

Before leaving the guest account, going to the emails again, we can see another email in the list, with a publication
number hidden inside:

```
INTRO.MUA=5@999999|b9666432feff66e528a17fb69ae8e9a
```

### User `ftd`

Reading the `README`, we find out about the 2 programs available here: `ml19100.exe` and `icfp.exe`.

Reading the `TODO`, we find out the password for the user `hmonk`: `COMEFROM`.

Running the `ml19100.exe` program, we get an exception but also a new publication:
```
BASIC.MLC=100@999999|8f8f7b233a9deb154cbcd5314b8e930
```

### User (TODO)

TODO

## Passwords summary

| Username   | Password        | 
|------------|-----------------|
| `ftd`      | `falderal90`    |
| `knr`      | ?               |
| `gardener` | ?               |
| `ohmega`   | `bidirectional` |
| `yang`     | ?               |
| `howie`    | `xyzzy`         |
| `hmonk`    | `COMEFROM`      |
| `bbarker`  | ?               |

## Publications summary

Check out the solution text for how we get each publication:

```
INTRO.LOG=200@999999|35e6f52e9bc951917c73af391e35e1d
INTRO.UMD=10@999999|7005f80d6cd9b7b837802f1e58b11b8
INTRO.MUA=5@999999|b9666432feff66e528a17fb69ae8e9a
BASIC.MLC=100@999999|8f8f7b233a9deb154cbcd5314b8e930
```
