const fs = require('fs');
const path = require('path');

function processFile(filePath) {
  const content = fs.readFileSync(filePath, 'utf8');
  const lines = content.split('\n');

  let inJavadoc = false;
  let output = [];
  let apiLines = [];
  let javadocBuffer = [];

  for (let line of lines) {
    if (line.trim().startsWith('/**')) {
      inJavadoc = true;
      javadocBuffer = [line];
      continue;
    }

    if (inJavadoc) {
      javadocBuffer.push(line);
      if (line.trim().startsWith('*/')) {
        inJavadoc = false;

        const cleanJavadoc = [];
        const apidocLines = [];

        for (let jdLine of javadocBuffer) {
          if (jdLine.match(/\*\s*@api/)) {
            apidocLines.push(jdLine.replace(/^\s*\*\s?/, ''));
          } else {
            cleanJavadoc.push(jdLine);
          }
        }

        output.push(...cleanJavadoc);

        if (apidocLines.length > 0) {
          output.push('/*');
          output.push(...apidocLines);
          output.push('*/');
        }

        javadocBuffer = [];
      }
      continue;
    }

    output.push(line);
  }

  fs.writeFileSync(filePath, output.join('\n'), 'utf8');
}

// Recursively process Java files in ./src
function processDirectory(dir) {
  fs.readdirSync(dir).forEach(file => {
    const fullPath = path.join(dir, file);
    if (fs.statSync(fullPath).isDirectory()) {
      processDirectory(fullPath);
    } else if (file.endsWith('.java')) {
      processFile(fullPath);
    }
  });
}

processDirectory(path.join(__dirname, '.'));
console.log('✅ JavaDoc cleanup complete.');
