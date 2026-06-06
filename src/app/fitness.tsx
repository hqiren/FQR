import React from "react";
import { StyleSheet, Text, View } from "react-native";

export default function Fitness() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>testing123</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  title: {
    fontSize: 20,
    fontWeight: "bold",
    marginBottom: 10,
  },
  subtitle: {
    fontSize: 16,
    color: "#666",
  },
});
