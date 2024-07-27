const fs = require('fs-extra');
const glob = require('glob');

function mergeDeep(a, b) {
    const keys = Object.keys(b);
    for (let i = 0; i < keys.length; i++) {
        const key = keys[i];

        if (a[key] === undefined) {
            a[key] = b[key];
            continue;
        }

        if (Array.isArray(a[key]) && Array.isArray(b[key])) {
            a[key] = b[key].concat(a[key]);
            continue;
        }
        
        if (a[key] instanceof Object && b[key] instanceof Object) {
            a[key] = mergeDeep(a[key], b[key]);
            continue;
        }
    }
    return a;
}

// extension directory 
const extensionDirectory = "homebrew"
const sourceDirectory = "../5etools-mirror-1.github.io/data"

// glob all json files in the source directory
const sourceFiles = glob.sync(`${sourceDirectory}/**/*.json`);

// create output directory data and ensure its empty if it exists
if (fs.existsSync('data')) {
    fs.emptyDirSync('data');
} else {
    fs.mkdirSync('data');
}

// for each source file, check if it exists in the extension directory, if so, merge the json objects recursively
sourceFiles.forEach((sourceFile) => {
    const extensionFile = sourceFile.replace(sourceDirectory, extensionDirectory);
    const sourceData = fs.readJsonSync(sourceFile);
    let resultData = sourceData;
    if (fs.existsSync(extensionFile)) {
        console.log(`Merging ${sourceFile} with ${extensionFile}`);
        const extensionData = fs.readJsonSync(extensionFile);
        const mergedData = mergeDeep(sourceData, extensionData);
        resultData = mergedData;
    }
    const outputFileName = sourceFile.replace(sourceDirectory, 'data');
    fs.outputJsonSync(outputFileName, resultData);
});
