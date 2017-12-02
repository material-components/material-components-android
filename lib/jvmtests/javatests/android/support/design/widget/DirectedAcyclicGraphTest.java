/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.design.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.support.annotation.NonNull;
import android.support.test.filters.SmallTest;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SmallTest
public class DirectedAcyclicGraphTest {

  private DirectedAcyclicGraph<TestNode> graph;

  @Before
  public void setup() {
    graph = new DirectedAcyclicGraph<>();
  }

  @Test
  public void test_addNode() {
    final TestNode node = new TestNode("node");
    graph.addNode(node);
    assertEquals(1, graph.size());
    assertTrue(graph.contains(node));
  }

  @Test
  public void test_addNodeAgain() {
    final TestNode node = new TestNode("node");
    graph.addNode(node);
    graph.addNode(node);

    assertEquals(1, graph.size());
    assertTrue(graph.contains(node));
  }

  @Test
  public void test_addEdge() {
    final TestNode node = new TestNode("node");
    final TestNode edge = new TestNode("edge");

    graph.addNode(node);
    graph.addNode(edge);
    graph.addEdge(node, edge);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_addEdgeWithNotAddedEdgeNode() {
    final TestNode node = new TestNode("node");
    final TestNode edge = new TestNode("edge");

    // Add the node, but not the edge node
    graph.addNode(node);

    // Now add the link
    graph.addEdge(node, edge);
  }

  @Test
  public void test_getIncomingEdges() {
    final TestNode node = new TestNode("node");
    final TestNode edge = new TestNode("edge");
    graph.addNode(node);
    graph.addNode(edge);
    graph.addEdge(node, edge);

    final List<TestNode> incomingEdges = graph.getIncomingEdges(node);
    assertNotNull(incomingEdges);
    assertEquals(1, incomingEdges.size());
    assertEquals(edge, incomingEdges.get(0));
  }

  @Test
  public void test_getOutgoingEdges() {
    final TestNode node = new TestNode("node");
    final TestNode edge = new TestNode("edge");
    graph.addNode(node);
    graph.addNode(edge);
    graph.addEdge(node, edge);

    // Now assert the getOutgoingEdges returns a list which has one element (node)
    final List<TestNode> outgoingEdges = graph.getOutgoingEdges(edge);
    assertNotNull(outgoingEdges);
    assertEquals(1, outgoingEdges.size());
    assertTrue(outgoingEdges.contains(node));
  }

  @Test
  public void test_getOutgoingEdgesMultiple() {
    final TestNode node1 = new TestNode("1");
    final TestNode node2 = new TestNode("2");
    final TestNode edge = new TestNode("edge");
    graph.addNode(node1);
    graph.addNode(node2);
    graph.addNode(edge);

    graph.addEdge(node1, edge);
    graph.addEdge(node2, edge);

    // Now assert the getOutgoingEdges returns a list which has 2 elements (node1 & node2)
    final List<TestNode> outgoingEdges = graph.getOutgoingEdges(edge);
    assertNotNull(outgoingEdges);
    assertEquals(2, outgoingEdges.size());
    assertTrue(outgoingEdges.contains(node1));
    assertTrue(outgoingEdges.contains(node2));
  }

  @Test
  public void test_hasOutgoingEdges() {
    final TestNode node = new TestNode("node");
    final TestNode edge = new TestNode("edge");
    graph.addNode(node);
    graph.addNode(edge);

    // There is no edge currently and assert that fact
    assertFalse(graph.hasOutgoingEdges(edge));
    // Now add the edge
    graph.addEdge(node, edge);
    // and assert that the methods returns true;
    assertTrue(graph.hasOutgoingEdges(edge));
  }

  @Test
  public void test_clear() {
    final TestNode node1 = new TestNode("1");
    final TestNode node2 = new TestNode("2");
    final TestNode edge = new TestNode("edge");
    graph.addNode(node1);
    graph.addNode(node2);
    graph.addNode(edge);

    // Now clear the graph
    graph.clear();

    // Now assert the graph is empty and that contains returns false
    assertEquals(0, graph.size());
    assertFalse(graph.contains(node1));
    assertFalse(graph.contains(node2));
    assertFalse(graph.contains(edge));
  }

  @Test
  public void test_getSortedList() {
    final TestNode node1 = new TestNode("A");
    final TestNode node2 = new TestNode("B");
    final TestNode node3 = new TestNode("C");
    final TestNode node4 = new TestNode("D");

    // Now we'll add the nodes
    graph.addNode(node1);
    graph.addNode(node2);
    graph.addNode(node3);
    graph.addNode(node4);

    // Now we'll add edges so that 4 <- 2, 2 <- 3, 3 <- 1  (where <- denotes a dependency)
    graph.addEdge(node4, node2);
    graph.addEdge(node2, node3);
    graph.addEdge(node3, node1);

    final List<TestNode> sorted = graph.getSortedList();
    // Assert that it is the correct size
    assertEquals(4, sorted.size());
    // Assert that all of the nodes are present and in their sorted order
    assertEquals(node1, sorted.get(0));
    assertEquals(node3, sorted.get(1));
    assertEquals(node2, sorted.get(2));
    assertEquals(node4, sorted.get(3));
  }

  private static class TestNode {
    private final String label;

    TestNode(@NonNull String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return "TestNode: " + label;
    }
  }
}
