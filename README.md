# Android Debloater

A pure Java Android Debloater.

> ⚠️ Use at your own risk. Make sure to back up your data before proceeding.

---

## 🚀 Features

- ✅ **Cross-platform**: Works on Windows, macOS, and Linux.
- 🔌 **Automatic ADB Setup**: Downloads and configures platform-tools automatically.
- 📦 **Curated Debloat Lists**: Community-maintained bloatware lists. (see: https://github.com/ycomiti/android-debloat-lists)
- 🖱️ **Graphical Interface**: Easy-to-use Swing-based desktop UI.
- 🔄 **Reversibility**: Changes can be undone via factory reset (in most devices).

---

## 📸 Screenshots

_Coming soon..._

---

## 🖥️ Requirements

- Java 17 or higher (JRE or JDK)
- ADB-compatible Android device with USB debugging enabled, and a USB cable — or connect wirelessly using the integrated pairing wizard.

---

## ❗️ Supported Platforms
Your host system (the computer running this debloater) must be compatible with Google's official ADB platform-tools. Below is a list of supported platforms and download links:

Windows	x86 / x64	No installation required	[Download](https://dl.google.com/android/repository/platform-tools-latest-windows.zip)<br/>
macOS	Intel (x86_64) & Apple Silicon (via Rosetta)	[Works natively or via Rosetta 2](https://dl.google.com/android/repository/platform-tools-latest-darwin.zip)<br/>
Linux	x86_64	May require udev rules for USB access	[Download](https://dl.google.com/android/repository/platform-tools-latest-linux.zip)<br/>

Other platforms like ARM-based Linux (e.g., Raspberry Pi) or BSD systems are not officially supported by Google’s ADB binaries.

---

## 🧩 Installation

Download the latest release from the [GitHub Releases](https://github.com/ycomiti/android-debloater/releases) page.

No installation is required on Windows as far i know.<br/>
On Linux, you may need to set up udev rules. (Mac support is untested.)<br/>
Just run the following command:

```bash
java -jar android-debloater.jar
````

The app will automatically download the necessary platform-tools based on your OS.

---

## 📦 Debloat Lists

This tool uses curated lists hosted [here](https://github.com/ycomiti/android-debloat-lists).

You can contribute by submitting pull requests or creating new issue reports.

---

## 🛡️ Disclaimer

> This software modifies system components on your Android device.<br/>
> Although most changes are reversible, there's always a small risk of soft-brick or bootloop.<br/>
> Always back up your important data before proceeding.<br/>

The author is not responsible for any damage caused by the use of this tool.

---

## 🤝 Contributing

Contributions are welcome!
Feel free to fork the repo, improve the code, or suggest features.

1. Fork the project
2. Create your feature branch (`git checkout -b my-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin my-feature`)
5. Create a pull request

---

## 📄 License

This project is licensed under the GNU General Public License v3.0 — see the LICENSE file for details.

---

## 🌐 Links

* **Project Repo**: [android-debloater](https://github.com/ycomiti/android-debloater)
* **Debloat Lists**: [android-debloat-lists](https://github.com/ycomiti/android-debloat-lists)
