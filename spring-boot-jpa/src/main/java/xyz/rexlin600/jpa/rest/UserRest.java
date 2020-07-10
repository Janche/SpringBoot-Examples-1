package xyz.rexlin600.jpa.rest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import xyz.rexlin600.jpa.common.apiparam.PageResult;
import xyz.rexlin600.jpa.common.apiparam.Response;
import xyz.rexlin600.jpa.common.apiparam.ResponseGenerator;
import xyz.rexlin600.jpa.entity.User;
import xyz.rexlin600.jpa.repository.UserRepository;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * User rest
 *
 * @author hekunlin
 */
@RestController
@RequestMapping("/jpa")
public class UserRest {

	/**
	 * User repository
	 */
	@Resource
	private UserRepository userRepository;

	/**
	 * List response
	 *
	 * @param page page
	 * @param size size
	 * @return the response
	 */
	@GetMapping
	public Response<PageResult<User>> list(@RequestParam(value = "page") Integer page,
										   @RequestParam(value = "size") Integer size) {
		Page<User> all = userRepository.findAll(PageRequest.of(page, size, Sort.Direction.ASC, "id"));

		PageResult<User> pageResult = new PageResult<>();
		pageResult.setTotalPage(all.getTotalPages());
		pageResult.setList(all.getContent());
		pageResult.setPage(page);
		pageResult.setSize(size);
		pageResult.setTotalCount(all.getTotalElements());
		return ResponseGenerator.success(pageResult);
	}

	/**
	 * Add response
	 *
	 * @param user user
	 * @return the response
	 */
	@PostMapping
	public Response add(@RequestBody User user) {
		userRepository.save(user);
		return ResponseGenerator.success();
	}

	/**
	 * Update response
	 *
	 * @param user user
	 * @return the response
	 */
	@PutMapping
	public Response update(@RequestBody User user) {
		userRepository.saveAndFlush(user);
		return ResponseGenerator.success();
	}


	/**
	 * Add response
	 *
	 * @param id id
	 * @return the response
	 */
	@GetMapping("/{id}")
	public Response add(@PathVariable(value = "id") Long id) {
		Optional<User> optional = userRepository.findById(id);
		if (optional.isPresent()) {
			return ResponseGenerator.success(optional.get());
		}
		return ResponseGenerator.fail("not exist this user");
	}


	/**
	 * Delete response
	 *
	 * @param id id
	 * @return the response
	 */
	@DeleteMapping("/{id}")
	public Response delete(@PathVariable(value = "id") Long id) {
		userRepository.deleteById(id);
		return ResponseGenerator.success();
	}


}