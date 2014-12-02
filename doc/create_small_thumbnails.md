for f in output/*/*thumb-1.png; do convert ${f/-thumb-1.png/-1.png} -trim -bordercolor white -border 3x3 $f; done

for f in output/*/*thumb-1.png; do convert ${f/-thumb-1.png/-1.png} -gravity Center -crop 99x99%+0+0 -trim -bordercolor white -border 3x3 $f; done
for f in output/Calligra/odp*thumb-1.png; do convert ${f/-thumb-1.png/-1.png} -gravity Center -crop 99x99%+0+0 -scale 35%x35%+0+0 -trim -bordercolor white -border 3x3 $f; done
