# You can uncomment the following lines to produce a png figure
#set terminal png enhanced
#set output 'plot.png'

set title "Average Path Length"
set xlabel "cycles"
set ylabel "average path length (log)"
set key right top
set logscale y
plot "pl30.txt" title 'Basic Shuffle c = 30' with lines, \
	"pl50.txt" title 'Basic Shuffle c = 50' with lines, \
	"random-pl30.txt" title 'Random c = 30' with lines, \
	"random-pl50.txt" title 'Random c = 50' with linesxt" title 'Random c = 50' with lines