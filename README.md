Spendthrift Project
=============

[![CI Status](https://github.com/caiormt/spendthrift/workflows/Build/badge.svg)](https://github.com/caiormt/spendthrift/workflows/Build/badge.svg)

## Installation

TODO: **ADD INFO**

### VSCode + Metals

[Metals with VSCode][metals-vscode] automatically detects the Mill Build.  
Just run: `Metals: Import build` on Visual Studio Code.

### IntelliJ IDEA

Install [Scala Plugin][intellij-scala] on IntelliJ.

### BSP

```shell script
./millw mill.bsp.BSP/install
```

### Dependecy Updates

```shell script
./millw mill.scalalib.Dependency/showUpdates
```

### Mill Updates

```shell script
./millw project.millw
```

### Lint

```shell script
./millw __.style
```

## Usage

TODO: Write usage instructions

## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

## History

Version 0.0.1 (TBD)

## Credits

Lead Developer - Caio Tokunaga (@caiormt)

## LICENSE

MIT License

Copyright (c) 2021 Caio Tokunaga

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

[mill]: https://com-lihaoyi.github.io/mill/mill
[metals-vscode]: https://scalameta.org/metals/docs/editors/vscode.html
[intellij-scala]: https://plugins.jetbrains.com/plugin/1347-scala
