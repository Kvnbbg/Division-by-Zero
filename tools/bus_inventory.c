/* bus_inventory.c — read-only presence summary for USB/PCI (authorized support)
 * © 2026 Kevin Marville · Tech & Stream · kvnbbg.fr
 *
 * Lists device *presence* from Linux sysfs when readable. Does not open
 * devices, does not bypass locks, does not escalate privileges.
 *
 * Build:  cc -O2 -o tools/bus_inventory tools/bus_inventory.c
 * Run:    ./tools/bus_inventory
 */

#include <dirent.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

static void list_dir(const char *path, const char *label)
{
    DIR *d = opendir(path);
    if (!d) {
        printf("[bus] %s: not readable (ok on non-Linux or restricted host)\n", label);
        return;
    }
    printf("[bus] %s (%s)\n", label, path);
    struct dirent *e;
    int n = 0;
    while ((e = readdir(d)) != NULL) {
        if (e->d_name[0] == '.')
            continue;
        printf("  - %s\n", e->d_name);
        if (++n >= 64) {
            printf("  ... truncated\n");
            break;
        }
    }
    closedir(d);
}

int main(void)
{
    printf("[bus] TDAAH authorized inventory — presence only\n");
    list_dir("/sys/bus/usb/devices", "USB");
    list_dir("/sys/bus/pci/devices", "PCI");
    if (access("/sys/bus/usb/devices", R_OK) != 0 &&
        access("/sys/bus/pci/devices", R_OK) != 0) {
        printf("[bus] No sysfs bus nodes; report host OS to IT if needed.\n");
        return 0;
    }
    return 0;
}
