# Troubleshooting

Common issues and solutions for nima applications.

## Controller Routes Not Found

**Symptom**: 404 for controller endpoints

**Solution**:
1. Verify `@Controller` annotation present
2. Check `@Path` annotation spelling
3. Ensure HTTP method annotation (`@Get`, `@Post`, etc.)
4. Check path variable syntax: `:id` not `{id}`

## Dependency Injection Errors

**Symptom**: `No bean found for type X`

**Solution**:
1. Verify `@Singleton` or `@Bean` annotation
2. Check for circular dependencies
3. Ensure interfaces have implementations
4. Use `@Named` qualifier if multiple implementations

## Validation Not Working

**Symptom**: Invalid data accepted

**Solution**:
1. Add bean validation annotations to request class
2. Call `validator.validate()` in controller
3. Check exception handler catches `ConstraintViolationException`

## Filters Not Called

**Symptom**: Filter logic never executes

**Solution**:
1. Verify filter registered with server
2. Check filter path conditions
3. Ensure `chain.doFilter()` called (except for final response)
4. Check filter ordering

## Performance Issues

**Symptom**: Slow request handling

**Solution**:
1. Profile with JFR (Java Flight Recorder)
2. Check for blocking I/O in handlers
3. Use connection pooling for database
4. Enable async operations for I/O
5. Build native image for startup performance

## Getting Help

- GitHub: https://github.com/avaje/avaje-nima
- Discord: https://discord.gg/Qcqf9R27BR
- Docs: https://avaje.io/nima/
