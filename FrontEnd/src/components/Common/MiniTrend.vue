<template>
  <div class="mini-trend">
    <svg :width="width" :height="height" :viewBox="`0 0 ${width} ${height}`">
      <path :d="path" :stroke="color" fill="none" stroke-width="2" />
      <circle :cx="maxPoint.x" :cy="maxPoint.y" r="2" :fill="color" />
      <circle :cx="minPoint.x" :cy="minPoint.y" r="2" :fill="color" />
    </svg>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  data: {
    type: Array,
    required: true,
    default: () => [],
  },
  color: {
    type: String,
    default: '#f56c6c',
  },
  width: {
    type: Number,
    default: 100,
  },
  height: {
    type: Number,
    default: 30,
  },
});

const maxVal = computed(() => Math.max(...props.data));
const minVal = computed(() => Math.min(...props.data));

const points = computed(() => {
  const data = props.data;
  const len = data.length;
  if (len < 2) return [];

  const width = props.width;
  const height = props.height;
  const range = maxVal.value - minVal.value;
  const scaleX = width / (len - 1);
  const scaleY = range > 0 ? (height - 4) / range : 0; // -4 for padding

  return data.map((val, i) => {
    const x = i * scaleX;
    const y = height - 2 - (val - minVal.value) * scaleY; // -2 for padding
    return { x, y };
  });
});

const path = computed(() => {
  if (points.value.length < 2) return '';
  const p = points.value;
  let path = `M ${p[0].x} ${p[0].y}`;
  for (let i = 1; i < p.length; i++) {
    const midX = (p[i-1].x + p[i].x) / 2;
    path += ` C ${midX} ${p[i-1].y}, ${midX} ${p[i].y}, ${p[i].x} ${p[i].y}`;
  }
  return path;
});

const maxPoint = computed(() => {
    if (points.value.length === 0) return {x:0, y:0};
    const maxV = maxVal.value;
    const maxIndex = props.data.indexOf(maxV);
    return points.value[maxIndex];
});

const minPoint = computed(() => {
    if (points.value.length === 0) return {x:0, y:0};
    const minV = minVal.value;
    const minIndex = props.data.indexOf(minV);
    return points.value[minIndex];
});

</script>

<style scoped>
.mini-trend {
  display: inline-block;
}
</style>
