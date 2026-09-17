/* ai_bootstrap.c — lightweight Unix/macOS AI CLI installer
 * © 2026 Kevin Marville · Tech & Stream · kvnbbg.fr
 * Public utility: detects apt / brew / pacman / dnf / yum and installs curl.
 * No secrets. Requires appropriate privileges for the chosen package manager.
 *
 * Build:  cc -O2 -o ai_bootstrap tools/ai_bootstrap.c
 * Run:    ./ai_bootstrap
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

static int run(const char *cmd)
{
    printf("[ai] %s\n", cmd);
    return system(cmd);
}

int main(void)
{
    if (access("/usr/bin/apt", X_OK) == 0) {
        return run("sudo apt update && sudo apt install -y curl");
    }

    if (access("/opt/homebrew/bin/brew", X_OK) == 0 ||
        access("/usr/local/bin/brew", X_OK) == 0) {
        return run("brew install curl");
    }

    if (access("/usr/bin/pacman", X_OK) == 0) {
        return run("sudo pacman -Sy --needed --noconfirm curl");
    }

    if (access("/usr/bin/dnf", X_OK) == 0) {
        return run("sudo dnf install -y curl");
    }

    if (access("/usr/bin/yum", X_OK) == 0) {
        return run("sudo yum install -y curl");
    }

    fprintf(stderr, "[ai] Unsupported Unix package manager\n");
    return 1;
}
