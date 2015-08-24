#!/usr/bin/gnuplot
#
# Plotting the data of file data.dat with points and non-continuous lines
#
# AUTHOR: Hagen Wierstorf
# VERSION: gnuplot 4.6 patchlevel 0

reset

# wxt
# set terminal wxt size 350,262 enhanced font 'Verdana,10' persist
# png
# set terminal pngcairo size 350,262 enhanced font 'Verdana,10'
#set output 'non-continuous_lines.png'

# color definitions
set border linewidth 1.5
# set style line 1 lc rgb '#0060ad' lt 1 lw 2 pt 7 pi -1 ps 1.5
set style line 1 lc rgb '#0060ad' lt 1 lw 2 pt 5 ps 1.5   # --- blue
set style line 2 lc rgb '#ada400' lt 1 lw 2 pt 7 ps 1.5   # --- blue
set style line 3 lc rgb '#ad0009' lt 1 lw 2 pt 9 ps 1.5   # --- blue

#set pointintervalbox 3
set datafile separator ","
unset key

set key autotitle columnheader

set xrange [*:*]
set yrange [0:1]

set xtics 0,.1,.4
set ytics 0,.2,1
#set tics scale 0.75

set xlabel "Noise"
set ylabel "Objective Function"

#plot 'plot.dat' i 0 u 1:2 w linespoints title columnheader(1)
#plot 'plot.dat' with linespoints ls 1 #title columnheader(1)
plot for [IDX=0:2] 'plot.dat' i IDX u 1:2 w linespoints ls (IDX+1) # title columnheader(1)
