<template>
  <div ref="graph"></div>
</template>

<script>
import * as d3 from 'd3';

export default {
  name: 'NetworkGraph',
  props: {
    nodes: {
      type: Array,
      required: true
    },
    links: {
      type: Array,
      required: true
    }
  },
  mounted() {
    this.drawGraph();
  },
  watch: {
    nodes: 'drawGraph',
    links: 'drawGraph'
  },
  methods: {
    drawGraph() {
      const width = 800;
      const height = 600;

      // 清空之前的SVG（以便重新绘制）
      d3.select(this.$refs.graph).selectAll('*').remove();

      const svg = d3.select(this.$refs.graph)
          .append('svg')
          .attr('width', width)
          .attr('height', height);

      // 定义箭头
      svg.append('defs').append('marker')
          .attr('id', 'arrowhead')
          .attr('viewBox', '-0 -5 10 10')
          .attr('refX', 25) // 增加箭头长度
          .attr('refY', 0)
          .attr('orient', 'auto')
          .attr('markerWidth', 10)
          .attr('markerHeight', 10)
          .attr('xoverflow', 'visible')
          .append('svg:path')
          .attr('d', 'M 0,-5 L 10 ,0 L 0,5')
          .attr('fill', '#999')
          .style('stroke', 'none');

      const simulation = d3.forceSimulation(this.nodes)
          .force('link', d3.forceLink(this.links).id(d => d.id).distance(150)) // 增加距离
          .force('charge', d3.forceManyBody().strength(-400))
          .force('center', d3.forceCenter(width / 2, height / 2));

      const link = svg.append('g')
          .attr('class', 'links')
          .selectAll('line')
          .data(this.links)
          .enter().append('line')
          .attr('stroke-width', 2)
          .attr('stroke', '#999')
          .attr('marker-end', 'url(#arrowhead)');

      // 添加关系上的文字
      const linkText = svg.append('g')
          .attr('class', 'link-labels')
          .selectAll('text')
          .data(this.links)
          .enter().append('text')
          .attr('font-size', 10)
          .attr('fill', '#555')
          .text(d => d.name);

      const node = svg.append('g')
          .attr('class', 'nodes')
          .selectAll('circle')
          .data(this.nodes)
          .enter().append('circle')
          .attr('r', 40) // 增大节点大小
          .attr('fill', '#69b3a2')
          .call(d3.drag()
              .on('start', dragstarted)
              .on('drag', dragged)
              .on('end', dragended));

      node.append('title')
          .text(d => d.name);

      // 在节点上显示name属性
      const nodeText = svg.append('g')
          .attr('class', 'node-labels')
          .selectAll('text')
          .data(this.nodes)
          .enter().append('text')
          .attr('dy', 5)
          .attr('text-anchor', 'middle')
          .attr('font-size', 12)
          .attr('fill', '#000')
          .text(d => d.name);

      simulation
          .nodes(this.nodes)
          .on('tick', ticked);

      simulation.force('link')
          .links(this.links);

      function ticked() {
        link
            .attr('x1', d => d.source.x)
            .attr('y1', d => d.source.y)
            .attr('x2', d => d.target.x)
            .attr('y2', d => d.target.y);

        linkText
            .attr('x', d => (d.source.x + d.target.x) / 2)
            .attr('y', d => (d.source.y + d.target.y) / 2);

        node
            .attr('cx', d => d.x)
            .attr('cy', d => d.y);

        nodeText
            .attr('x', d => d.x)
            .attr('y', d => d.y);
      }

      function dragstarted(event, d) {
        if (!event.active) simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
      }

      function dragged(event, d) {
        d.fx = event.x;
        d.fy = event.y;
      }

      function dragended(event, d) {
        if (!event.active) simulation.alphaTarget(0);
        d.fx = d.x;
        d.fy = d.y; // 保持节点拖动后位置不变
      }
    }
  }
};
</script>

<style scoped>
.links line {
  stroke: #999;
  stroke-opacity: 0.6;
}

.nodes circle {
  stroke: #fff;
  stroke-width: 1.5px;
}

.link-labels text {
  pointer-events: none;
}

.node-labels text {
  pointer-events: none;
}
</style>
