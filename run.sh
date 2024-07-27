#!/bin/zsh

node run.js

fc5-convert 5etools --xml --md --index -o export-players -s PHB,MM,MPMM,TCE,VGM,XGE,DMG,GGR,SCC,SCAG,ERLW,MOT,VRGR,BGG,BMT,FTD,HOMEBREW data
fc5-convert 5etools --xml --md --index -o export-dm -s PHB,MM,MPMM,TCE,VGM,XGE,DMG,GGR,SCC,SCAG,ERLW,MOT,VRGR,BGG,BMT,FTD,HOMEBREW,HOMEBREWDM data
